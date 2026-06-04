import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { viteSingleFile } from 'vite-plugin-singlefile';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react(), tailwindcss(), viteSingleFile()],
  resolve: { alias: { '@': resolve(__dirname, './src') } },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: { input: resolve(__dirname, 'index.html') },
  },
});
