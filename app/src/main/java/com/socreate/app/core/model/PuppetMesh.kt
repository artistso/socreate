package com.socreate.app.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Puppet Mesh Tools — advanced mesh-based puppet deformation system.
 *
 * Inspired by Aseprite/Resprite puppet tools and Clip Studio Paint's
 * Puppet Warp. Provides a triangulated mesh that can be deformed by
 * dragging control points, with real-time GPU preview.
 *
 * Features:
 * - Auto-generate mesh from selection/layer content
 * - Manual pin placement (joints, anchors)
 * - Mesh density control (coarse ↔ fine)
 * - Real-time deformation preview
 * - Keyframe-able mesh poses for animation
 * - Bone-like rigging with parent-child pin chains
 * - Mesh interpolation between keyframes
 * - Rig presets for common characters
 * - Symmetry-aware mesh editing
 * - Import mesh from SVG paths
 */
@Serializable
data class PuppetMesh(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Puppet",
    val vertices: List<MeshVertex> = emptyList(),
    val edges: List<MeshEdge> = emptyList(),
    val triangles: List<MeshTriangle> = emptyList(),
    val pins: List<MeshPin> = emptyList(),
    val bones: List<MeshBone> = emptyList(),
    val bounds: Bounds = Bounds.ZERO,
    val density: MeshDensity = MeshDensity.MEDIUM,
    val sourceLayerId: String? = null,
    val sourceBitmapPath: String? = null,
    val isSymmetric: Boolean = false,
    val symmetryAxis: Float = 0.5f  // 0..1 normalized X position
) {
    val vertexCount: Int get() = vertices.size
    val triangleCount: Int get() = triangles.size
    val pinCount: Int get() = pins.size
    val boneCount: Int get() = bones.size

    /** Find nearest pin to a given canvas coordinate */
    fun nearestPin(x: Float, y: Float, maxDistance: Float = 30f): MeshPin? {
        return pins.minByOrNull { pin ->
            val dx = pin.currentX - x
            val dy = pin.currentY - y
            kotlin.math.sqrt(dx * dx + dy * dy)
        }?.take { pin ->
            val dx = pin.currentX - x
            val dy = pin.currentY - y
            kotlin.math.sqrt(dx * dx + dy * dy) <= maxDistance
        }
    }

    /** Find nearest bone to a given canvas coordinate */
    fun nearestBone(x: Float, y: Float, maxDistance: Float = 30f): MeshBone? {
        return bones.minByOrNull { bone ->
            distanceToBone(x, y, bone)
        }?.take { bone ->
            distanceToBone(x, y, bone) <= maxDistance
        }
    }

    private fun distanceToBone(x: Float, y: Float, bone: MeshBone): Float {
        val startPin = pins.find { it.id == bone.startPinId } ?: return Float.MAX_VALUE
        val endPin = pins.find { it.id == bone.endPinId } ?: return Float.MAX_VALUE
        val dx = endPin.currentX - startPin.currentX
        val dy = endPin.currentY - startPin.currentY
        val len2 = dx * dx + dy * dy
        if (len2 == 0f) return kotlin.math.sqrt(
            (x - startPin.currentX) * (x - startPin.currentX) +
            (y - startPin.currentY) * (y - startPin.currentY)
        )
        var t = ((x - startPin.currentX) * dx + (y - startPin.currentY) * dy) / len2
        t = t.coerceIn(0f, 1f)
        val projX = startPin.currentX + t * dx
        val projY = startPin.currentY + t * dy
        return kotlin.math.sqrt((x - projX) * (x - projX) + (y - projY) * (y - projY))
    }
}

@Serializable
data class MeshVertex(
    val id: String = UUID.randomUUID().toString(),
    val originalX: Float,
    val originalY: Float,
    val currentX: Float = originalX,
    val currentY: Float = originalY,
    val weight: Float = 1f,
    val isFixed: Boolean = false,
    val uvX: Float = 0f,  // Texture coordinate for GPU
    val uvY: Float = 0f
)

@Serializable
data class MeshEdge(
    val id: String = UUID.randomUUID().toString(),
    val vertexA: String,  // Vertex ID
    val vertexB: String,  // Vertex ID
    val restLength: Float = 0f,
    val stiffness: Float = 1f
)

@Serializable
data class MeshTriangle(
    val id: String = UUID.randomUUID().toString(),
    val vertexA: String,  // Vertex ID
    val vertexB: String,  // Vertex ID
    val vertexC: String,  // Vertex ID
    val area: Float = 0f
)

@Serializable
data class MeshPin(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val originalX: Float,
    val originalY: Float,
    val currentX: Float = originalX,
    val currentY: Float = originalY,
    val type: PinType = PinType.JOINT,
    val isFixed: Boolean = false,
    val stiffness: Float = 1f,
    val affectedVertices: List<String> = emptyList(),  // Vertex IDs within influence
    val influenceRadius: Float = 100f,
    val parentId: String? = null,  // For bone chains
    val color: SoCreateColor = SoCreateColor(1f, 0.3f, 0.5f),
    val rotation: Float = 0f,  // Degrees, for bone direction
    val isConnectedToMesh: Boolean = true
)

@Serializable
enum class PinType(val displayName: String) {
    JOINT("Joint"),              // Standard pin at joint positions
    ANCHOR("Anchor"),            // Fixed point (doesn't move)
    EFFECTOR("Effector"),        // End-effector (e.g., hand, foot)
    CONTROL("Control"),          // User control handle
    TWEAK("Tweak"),              // Small adjustment point
    IK_HANDLE("IK Handle")       // Inverse kinematics target
}

@Serializable
data class MeshBone(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val startPinId: String,
    val endPinId: String,
    val parentBoneId: String? = null,
    val childBoneIds: List<String> = emptyList(),
    val stiffness: Float = 0.8f,
    val damping: Float = 0.5f,
    val length: Float = 0f,
    val color: SoCreateColor = SoCreateColor(0.2f, 0.8f, 1f),
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val ikEnabled: Boolean = false,
    val ikChainLength: Int = 2
)

@Serializable
enum class MeshDensity(val divisions: Int, val displayName: String) {
    VERY_COARSE(4, "Very Coarse"),
    COARSE(8, "Coarse"),
    MEDIUM(12, "Medium"),
    FINE(20, "Fine"),
    VERY_FINE(32, "Very Fine"),
    ULTRA(48, "Ultra")
}

// ─── Rig Presets ────────────────────────────────────────────────────────────

@Serializable
data class RigPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val pinPositions: List<PresetPin> = emptyList(),
    val boneConnections: List<PresetBone> = emptyList(),
    val canvasWidth: Int = 2800,
    val canvasHeight: Int = 1752
)

@Serializable
data class PresetPin(
    val name: String,
    val normalizedX: Float,  // 0..1
    val normalizedY: Float,  // 0..1
    val type: PinType = PinType.JOINT
)

@Serializable
data class PresetBone(
    val name: String,
    val startPinName: String,
    val endPinName: String,
    val parentBoneName: String? = null
)

object RigPresets {

    /** Simple humanoid biped rig */
    val HUMANOID = RigPreset(
        name = "Humanoid Biped",
        description = "Full body humanoid rig with IK-ready limbs",
        pinPositions = listOf(
            // Spine
            PresetPin("hips", 0.5f, 0.55f, PinType.ANCHOR),
            PresetPin("spine", 0.5f, 0.45f),
            PresetPin("chest", 0.5f, 0.35f),
            PresetPin("neck", 0.5f, 0.28f),
            PresetPin("head", 0.5f, 0.18f, PinType.EFFECTOR),
            // Left arm
            PresetPin("l_shoulder", 0.4f, 0.33f),
            PresetPin("l_elbow", 0.32f, 0.38f),
            PresetPin("l_wrist", 0.24f, 0.43f, PinType.IK_HANDLE),
            PresetPin("l_hand", 0.22f, 0.44f, PinType.EFFECTOR),
            // Right arm
            PresetPin("r_shoulder", 0.6f, 0.33f),
            PresetPin("r_elbow", 0.68f, 0.38f),
            PresetPin("r_wrist", 0.76f, 0.43f, PinType.IK_HANDLE),
            PresetPin("r_hand", 0.78f, 0.44f, PinType.EFFECTOR),
            // Left leg
            PresetPin("l_hip", 0.46f, 0.56f),
            PresetPin("l_knee", 0.45f, 0.68f),
            PresetPin("l_ankle", 0.44f, 0.80f, PinType.IK_HANDLE),
            PresetPin("l_foot", 0.43f, 0.82f, PinType.EFFECTOR),
            // Right leg
            PresetPin("r_hip", 0.54f, 0.56f),
            PresetPin("r_knee", 0.55f, 0.68f),
            PresetPin("r_ankle", 0.56f, 0.80f, PinType.IK_HANDLE),
            PresetPin("r_foot", 0.57f, 0.82f, PinType.EFFECTOR)
        ),
        boneConnections = listOf(
            PresetBone("spine", "hips", "spine"),
            PresetBone("chest", "spine", "chest"),
            PresetBone("neck", "chest", "neck"),
            PresetBone("head", "neck", "head"),
            PresetBone("l_upper_arm", "l_shoulder", "l_elbow", "chest"),
            PresetBone("l_forearm", "l_elbow", "l_wrist", "l_upper_arm"),
            PresetBone("l_hand_bone", "l_wrist", "l_hand", "l_forearm"),
            PresetBone("r_upper_arm", "r_shoulder", "r_elbow", "chest"),
            PresetBone("r_forearm", "r_elbow", "r_wrist", "r_upper_arm"),
            PresetBone("r_hand_bone", "r_wrist", "r_hand", "r_forearm"),
            PresetBone("l_upper_leg", "l_hip", "l_knee", "hips"),
            PresetBone("l_lower_leg", "l_knee", "l_ankle", "l_upper_leg"),
            PresetBone("l_foot_bone", "l_ankle", "l_foot", "l_lower_leg"),
            PresetBone("r_upper_leg", "r_hip", "r_knee", "hips"),
            PresetBone("r_lower_leg", "r_knee", "r_ankle", "r_upper_leg"),
            PresetBone("r_foot_bone", "r_ankle", "r_foot", "r_lower_leg")
        )
    )

    /** Simple quadruped rig (dog/cat) */
    val QUADRUPED = RigPreset(
        name = "Quadruped",
        description = "Four-legged animal rig",
        pinPositions = listOf(
            PresetPin("hips", 0.6f, 0.45f, PinType.ANCHOR),
            PresetPin("spine_mid", 0.5f, 0.40f),
            PresetPin("shoulders", 0.4f, 0.42f),
            PresetPin("neck", 0.33f, 0.38f),
            PresetPin("head", 0.25f, 0.32f, PinType.EFFECTOR),
            // Front legs
            PresetPin("fl_shoulder", 0.38f, 0.44f),
            PresetPin("fl_knee", 0.37f, 0.58f),
            PresetPin("fl_ankle", 0.36f, 0.72f, PinType.EFFECTOR),
            PresetPin("fr_shoulder", 0.42f, 0.44f),
            PresetPin("fr_knee", 0.43f, 0.58f),
            PresetPin("fr_ankle", 0.44f, 0.72f, PinType.EFFECTOR),
            // Back legs
            PresetPin("bl_hip", 0.58f, 0.47f),
            PresetPin("bl_knee", 0.56f, 0.60f),
            PresetPin("bl_ankle", 0.55f, 0.72f, PinType.EFFECTOR),
            PresetPin("br_hip", 0.62f, 0.47f),
            PresetPin("br_knee", 0.64f, 0.60f),
            PresetPin("br_ankle", 0.65f, 0.72f, PinType.EFFECTOR),
            // Tail
            PresetPin("tail_base", 0.68f, 0.42f),
            PresetPin("tail_mid", 0.75f, 0.38f),
            PresetPin("tail_tip", 0.82f, 0.34f, PinType.EFFECTOR)
        ),
        boneConnections = listOf(
            PresetBone("spine", "hips", "spine_mid"),
            PresetBone("chest", "spine_mid", "shoulders"),
            PresetBone("neck_bone", "shoulders", "neck"),
            PresetBone("head_bone", "neck", "head"),
            PresetBone("fl_upper", "fl_shoulder", "fl_knee"),
            PresetBone("fl_lower", "fl_knee", "fl_ankle"),
            PresetBone("fr_upper", "fr_shoulder", "fr_knee"),
            PresetBone("fr_lower", "fr_knee", "fr_ankle"),
            PresetBone("bl_upper", "bl_hip", "bl_knee"),
            PresetBone("bl_lower", "bl_knee", "bl_ankle"),
            PresetBone("br_upper", "br_hip", "br_knee"),
            PresetBone("br_lower", "br_knee", "br_ankle"),
            PresetBone("tail_1", "tail_base", "tail_mid"),
            PresetBone("tail_2", "tail_mid", "tail_tip")
        )
    )

    /** Simple face rig for expressions */
    val FACE = RigPreset(
        name = "Face / Expressions",
        description = "Facial rig for expressions and lip sync",
        pinPositions = listOf(
            PresetPin("head_center", 0.5f, 0.4f, PinType.ANCHOR),
            // Eyes
            PresetPin("l_eye_inner", 0.42f, 0.35f),
            PresetPin("l_eye_outer", 0.38f, 0.36f),
            PresetPin("l_eyelid_top", 0.40f, 0.33f, PinType.CONTROL),
            PresetPin("l_eyebrow", 0.40f, 0.28f, PinType.CONTROL),
            PresetPin("r_eye_inner", 0.58f, 0.35f),
            PresetPin("r_eye_outer", 0.62f, 0.36f),
            PresetPin("r_eyelid_top", 0.60f, 0.33f, PinType.CONTROL),
            PresetPin("r_eyebrow", 0.60f, 0.28f, PinType.CONTROL),
            // Nose
            PresetPin("nose_bridge", 0.5f, 0.36f),
            PresetPin("nose_tip", 0.5f, 0.42f),
            // Mouth
            PresetPin("mouth_l", 0.43f, 0.50f, PinType.CONTROL),
            PresetPin("mouth_r", 0.57f, 0.50f, PinType.CONTROL),
            PresetPin("mouth_top", 0.5f, 0.48f, PinType.CONTROL),
            PresetPin("mouth_bottom", 0.5f, 0.53f, PinType.CONTROL),
            PresetPin("jaw", 0.5f, 0.58f, PinType.CONTROL),
            // Ears
            PresetPin("l_ear", 0.30f, 0.38f),
            PresetPin("r_ear", 0.70f, 0.38f)
        ),
        boneConnections = listOf(
            PresetBone("l_eye", "l_eye_inner", "l_eye_outer"),
            PresetBone("r_eye", "r_eye_inner", "r_eye_outer"),
            PresetBone("nose", "nose_bridge", "nose_tip"),
            PresetBone("mouth", "mouth_l", "mouth_r"),
            PresetBone("jaw_bone", "mouth_bottom", "jaw")
        )
    )

    /** Simple hand rig for detailed hand animation */
    val HAND = RigPreset(
        name = "Hand",
        description = "Detailed hand rig with 5 fingers",
        pinPositions = listOf(
            PresetPin("wrist", 0.5f, 0.75f, PinType.ANCHOR),
            PresetPin("palm_center", 0.5f, 0.60f),
            // Thumb
            PresetPin("thumb_1", 0.38f, 0.58f),
            PresetPin("thumb_2", 0.30f, 0.50f),
            PresetPin("thumb_tip", 0.25f, 0.42f, PinType.EFFECTOR),
            // Index
            PresetPin("index_1", 0.40f, 0.48f),
            PresetPin("index_2", 0.38f, 0.36f),
            PresetPin("index_3", 0.37f, 0.26f, PinType.EFFECTOR),
            // Middle
            PresetPin("mid_1", 0.48f, 0.45f),
            PresetPin("mid_2", 0.48f, 0.32f),
            PresetPin("mid_3", 0.48f, 0.22f, PinType.EFFECTOR),
            // Ring
            PresetPin("ring_1", 0.55f, 0.46f),
            PresetPin("ring_2", 0.56f, 0.34f),
            PresetPin("ring_3", 0.57f, 0.24f, PinType.EFFECTOR),
            // Pinky
            PresetPin("pinky_1", 0.62f, 0.48f),
            PresetPin("pinky_2", 0.64f, 0.38f),
            PresetPin("pinky_3", 0.65f, 0.30f, PinType.EFFECTOR)
        ),
        boneConnections = listOf(
            PresetBone("palm", "wrist", "palm_center"),
            PresetBone("thumb_1", "palm_center", "thumb_1"),
            PresetBone("thumb_2", "thumb_1", "thumb_2"),
            PresetBone("thumb_3", "thumb_2", "thumb_tip"),
            PresetBone("index_1", "palm_center", "index_1"),
            PresetBone("index_2", "index_1", "index_2"),
            PresetBone("index_3", "index_2", "index_3"),
            PresetBone("mid_1", "palm_center", "mid_1"),
            PresetBone("mid_2", "mid_1", "mid_2"),
            PresetBone("mid_3", "mid_2", "mid_3"),
            PresetBone("ring_1", "palm_center", "ring_1"),
            PresetBone("ring_2", "ring_1", "ring_2"),
            PresetBone("ring_3", "ring_2", "ring_3"),
            PresetBone("pinky_1", "palm_center", "pinky_1"),
            PresetBone("pinky_2", "pinky_1", "pinky_2"),
            PresetBone("pinky_3", "pinky_2", "pinky_3")
        )
    )

    val ALL = listOf(HUMANOID, QUADRUPED, FACE, HAND)
    fun getById(id: String): RigPreset? = ALL.find { it.id == id }
}

// ─── Mesh Pose (for keyframing) ─────────────────────────────────────────────

@Serializable
data class MeshPose(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Pose",
    val meshId: String,
    val pinPositions: Map<String, PinPose> = emptyMap(),  // Pin ID → Pose
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class PinPose(
    val pinId: String,
    val x: Float,
    val y: Float,
    val rotation: Float = 0f,
    val scale: Float = 1f
)

// ─── Mesh Generation Config ─────────────────────────────────────────────────

@Serializable
data class MeshGenConfig(
    val density: MeshDensity = MeshDensity.MEDIUM,
    val respectAlpha: Boolean = true,
    val alphaThreshold: Float = 0.1f,
    val padding: Float = 10f,
    val maxVertices: Int = 2000,
    val smoothBoundaries: Boolean = true,
    val symmetryAware: Boolean = false,
    val generateBones: Boolean = false,
    val rigPresetId: String? = null
)

// ─── Mesh Deform Tool ───────────────────────────────────────────────────────

@Serializable
enum class MeshTool(val displayName: String) {
    SELECT("Select Pin"),
    MOVE("Move Pin"),
    ADD_PIN("Add Pin"),
    REMOVE_PIN("Remove Pin"),
    ADD_BONE("Add Bone"),
    REMOVE_BONE("Remove Bone"),
    PAINT_WEIGHT("Paint Weight"),
    ERASE_WEIGHT("Erase Weight"),
    POSE("Pose"),
    GENERATE("Generate Mesh"),
    ADJUST_DENSITY("Adjust Density")
}
