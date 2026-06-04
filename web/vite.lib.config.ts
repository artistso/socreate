import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import dts from 'vite-plugin-dts';
import { resolve } from 'path';
import { libInjectCss } from 'vite-plugin-lib-inject-css';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    libInjectCss(),
    dts({
      include: ['src'],
      outDir: 'dist/types',
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
  build: {
    outDir: 'dist',
    emptyOutDir: false,
    lib: {
      entry: resolve(__dirname, 'src/index.ts'),
      name: 'SoCreate',
      formats: ['es', 'cjs', 'umd'],
      fileName: (format) => `socreate.${format === 'es' ? 'mjs' : format === 'cjs' ? 'js' : 'umd.js'}`,
    },
    rollupOptions: {
      external: ['react', 'react-dom', 'zustand', 'uuid'],
      output: {
        globals: {
          react: 'React',
          'react-dom': 'ReactDOM',
          zustand: 'zustand',
          uuid: 'uuid',
        },
      },
    },
    sourcemap: true,
    minify: 'terser',
  },
});
