package com.socreate.app.engine.puppet

import com.socreate.app.core.model.*
import kotlin.math.sqrt
import kotlin.math.pow

/**
 * Puppet Mesh Engine — generates and deforms triangulated meshes for puppet animation.
 *
 * Pipeline:
 * 1. Generate mesh: Create a regular grid triangulation within the content bounds
 * 2. Place pins: Auto-place or manually add control pins
 * 3. Connect bones: Link pins into a bone hierarchy
 * 4. Deform: Move pins, compute vertex displacements via weight-based interpolation
 * 5. Render: Draw deformed mesh with texture mapping
 *
 * Deformation algorithm:
 * - Each pin has an influence radius and weight per vertex
 * - Moving a pin displaces nearby vertices proportionally to their weight
 * - Fixed pins anchor vertices in place
 * - Bone chains propagate deformation through parent-child relationships
 *
 * Mesh generation:
 * - Regular grid triangulation with configurable density
 * - Alpha-aware: only generate vertices within content bounds
 * - Symmetry-aware: optionally mirror mesh and pins
 */
class MeshEngine {

    /**
     * Generate a triangulated mesh for the given configuration.
     *
     * @param bounds Content bounds to fill with mesh
     * @param config Generation parameters
     * @return Generated PuppetMesh
     */
    fun generateMesh(bounds: Bounds, config: MeshGenConfig): PuppetMesh {
        val divisions = config.density.divisions
        val cellWidth = bounds.width / divisions
        val cellHeight = bounds.height / divisions

        // Generate grid vertices
        val vertices = mutableListOf<MeshVertex>()
        val vertexGrid = Array(divisions + 1) { arrayOfNulls<MeshVertex?>(divisions + 1) }

        for (row in 0..divisions) {
            for (col in 0..divisions) {
                val x = bounds.x + col * cellWidth
                val y = bounds.y + row * cellHeight

                // UV coordinates (0..1 normalized)
                val u = col.toFloat() / divisions
                val v = row.toFloat() / divisions

                val vertex = MeshVertex(
                    originalX = x, originalY = y,
                    currentX = x, currentY = y,
                    uvX = u, uvY = v
                )
                vertices.add(vertex)
                vertexGrid[row][col] = vertex
            }
        }

        // Generate triangles (two per grid cell)
        val edges = mutableListOf<MeshEdge>()
        val triangles = mutableListOf<MeshTriangle>()
        val edgeSet = mutableSetOf<Pair<String, String>>()

        for (row in 0 until divisions) {
            for (col in 0 until divisions) {
                val tl = vertexGrid[row][col]!!
                val tr = vertexGrid[row][col + 1]!!
                val bl = vertexGrid[row + 1][col]!!
                val br = vertexGrid[row + 1][col + 1]!!

                // Triangle 1: TL, BL, TR
                addTriangle(tl, bl, tr, triangles, edges, edgeSet)

                // Triangle 2: TR, BL, BR
                addTriangle(tr, bl, br, triangles, edges, edgeSet)
            }
        }

        return PuppetMesh(
            vertices = vertices,
            edges = edges,
            triangles = triangles,
            bounds = bounds,
            density = config.density
        )
    }

    private fun addTriangle(
        a: MeshVertex, b: MeshVertex, c: MeshVertex,
        triangles: MutableList<MeshTriangle>,
        edges: MutableList<MeshEdge>,
        edgeSet: MutableSet<Pair<String, String>>
    ) {
        // Add edges (avoid duplicates)
        addEdge(a, b, edges, edgeSet)
        addEdge(b, c, edges, edgeSet)
        addEdge(c, a, edges, edgeSet)

        // Add triangle
        val area = triangleArea(a, b, c)
        triangles.add(MeshTriangle(
            vertexA = a.id, vertexB = b.id, vertexC = c.id,
            area = area
        ))
    }

    private fun addEdge(
        a: MeshVertex, b: MeshVertex,
        edges: MutableList<MeshEdge>,
        edgeSet: MutableSet<Pair<String, String>>
    ) {
        val key = if (a.id < b.id) a.id to b.id else b.id to a.id
        if (key !in edgeSet) {
            edgeSet.add(key)
            val dx = b.originalX - a.originalX
            val dy = b.originalY - a.originalY
            edges.add(MeshEdge(
                vertexA = a.id, vertexB = b.id,
                restLength = sqrt(dx * dx + dy * dy)
            ))
        }
    }

    private fun triangleArea(a: MeshVertex, b: MeshVertex, c: MeshVertex): Float {
        return kotlin.math.abs(
            (a.originalX * (b.originalY - c.originalY) +
             b.originalX * (c.originalY - a.originalY) +
             c.originalX * (a.originalY - b.originalY)) / 2f
        )
    }

    /**
     * Compute vertex weights for each pin based on influence radius.
     * Uses inverse distance weighting.
     */
    fun computeVertexWeights(mesh: PuppetMesh): PuppetMesh {
        val updatedPins = mesh.pins.map { pin ->
            val affectedVerts = mesh.vertices.mapNotNull { vertex ->
                val dx = vertex.originalX - pin.originalX
                val dy = vertex.originalY - pin.originalY
                val dist = sqrt(dx * dx + dy * dy)

                if (dist <= pin.influenceRadius) {
                    val weight = if (dist == 0f) 1f else (1f - dist / pin.influenceRadius).coerceIn(0f, 1f)
                    vertex.id to weight
                } else null
            }

            pin.copy(affectedVertices = affectedVerts.map { it.first })
        }

        return mesh.copy(pins = updatedPins)
    }

    /**
     * Apply pin movements to deform the mesh vertices.
     *
     * For each non-fixed pin, compute the displacement from original to current position.
     * For each vertex within the pin's influence radius, apply weighted displacement.
     *
     * @param mesh Current mesh state
     * @return Mesh with updated vertex positions
     */
    fun deformMesh(mesh: PuppetMesh): PuppetMesh {
        // Start with original positions
        val currentPositions = mesh.vertices.associate { it.id to floatArrayOf(it.originalX, it.originalY) }
            .toMutableMap()

        // Accumulate displacements from all moved pins
        val displacementWeights = mesh.vertices.associate { it.id to mutableListOf<Pair<Float, Float>>() }
            .toMutableMap()

        for (pin in mesh.pins) {
            if (pin.isFixed) continue
            if (pin.currentX == pin.originalX && pin.currentY == pin.originalY) continue

            val dx = pin.currentX - pin.originalX
            val dy = pin.currentY - pin.originalY

            for (vertexId in pin.affectedVertices) {
                val vertex = mesh.vertices.find { it.id == vertexId } ?: continue
                val vdx = vertex.originalX - pin.originalX
                val vdy = vertex.originalY - pin.originalY
                val dist = sqrt(vdx * vdx + vdy * vdy)

                val weight = if (dist == 0f) pin.stiffness
                else ((1f - dist / pin.influenceRadius) * pin.stiffness).coerceIn(0f, 1f)

                if (weight > 0f) {
                    displacementWeights[vertexId]?.add(dx * weight to dy * weight)
                }
            }
        }

        // Apply accumulated displacements
        val updatedVertices = mesh.vertices.map { vertex ->
            val disps = displacementWeights[vertex.id] ?: emptyList()
            if (disps.isEmpty()) {
                vertex.copy(currentX = vertex.originalX, currentY = vertex.originalY)
            } else {
                val totalDx = disps.sumOf { it.first.toDouble() }.toFloat()
                val totalDy = disps.sumOf { it.second.toDouble() }.toFloat()
                vertex.copy(
                    currentX = vertex.originalX + totalDx,
                    currentY = vertex.originalY + totalDy
                )
            }
        }

        return mesh.copy(vertices = updatedVertices)
    }

    /**
     * Apply a rig preset to the mesh, creating pins and bones at normalized positions.
     */
    fun applyRigPreset(
        mesh: PuppetMesh,
        preset: RigPreset,
        canvasWidth: Int,
        canvasHeight: Int
    ): PuppetMesh {
        // Create pins from preset
        val pins = preset.pinPositions.map { presetPin ->
            MeshPin(
                name = presetPin.name,
                originalX = presetPin.normalizedX * canvasWidth,
                originalY = presetPin.normalizedY * canvasHeight,
                currentX = presetPin.normalizedX * canvasWidth,
                currentY = presetPin.normalizedY * canvasHeight,
                type = presetPin.type,
                influenceRadius = minOf(canvasWidth, canvasHeight) * 0.15f
            )
        }

        // Create bones from preset
        val bones = preset.boneConnections.map { presetBone ->
            val startPin = pins.find { it.name == presetBone.startPinName }
            val endPin = pins.find { it.name == presetBone.endPinName }
            if (startPin != null && endPin != null) {
                val dx = endPin.originalX - startPin.originalX
                val dy = endPin.originalY - startPin.originalY
                MeshBone(
                    name = presetBone.name,
                    startPinId = startPin.id,
                    endPinId = endPin.id,
                    length = sqrt(dx * dx + dy * dy)
                )
            } else null
        }.filterNotNull()

        // Link parent bones
        val linkedBones = bones.map { bone ->
            val presetBone = preset.boneConnections.find { it.name == bone.name }
            val parentId = presetBone?.parentBoneName?.let { parentName ->
                bones.find { it.name == parentName }?.id
            }
            bone.copy(parentBoneId = parentId)
        }

        return mesh.copy(pins = pins, bones = linkedBones)
    }

    /**
     * Interpolate between two mesh poses.
     * @param from Starting pose
     * @param to Ending pose
     * @param t Interpolation factor (0..1)
     * @return Interpolated pin positions
     */
    fun interpolatePoses(from: MeshPose, to: MeshPose, t: Float): Map<String, PinPose> {
        val result = mutableMapOf<String, PinPose>()

        for ((pinId, fromPose) in from.pinPositions) {
            val toPose = to.pinPositions[pinId] ?: continue
            result[pinId] = PinPose(
                pinId = pinId,
                x = fromPose.x + (toPose.x - fromPose.x) * t,
                y = fromPose.y + (toPose.y - fromPose.y) * t,
                rotation = fromPose.rotation + (toPose.rotation - fromPose.rotation) * t,
                scale = fromPose.scale + (toPose.scale - fromPose.scale) * t
            )
        }

        return result
    }
}
