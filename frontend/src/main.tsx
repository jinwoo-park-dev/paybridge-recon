import React from 'react';
import { createRoot } from 'react-dom/client';
import { WorkbenchApp } from './app/WorkbenchApp.js';

const rootElement = document.getElementById('recon-workbench-root');

if (rootElement) {
  createRoot(rootElement).render(<WorkbenchApp />);
}
