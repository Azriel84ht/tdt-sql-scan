import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './routes'
import { Toaster } from 'react-hot-toast'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Toaster
      position="top-right"
      toastOptions={{
        className: '',
        style: {
          border: '1px solid #713200',
          padding: '16px',
          color: '#f0f0f0',
          backgroundColor: '#1a1a1a',
        },
        error: {
          iconTheme: {
            primary: '#ff4b4b',
            secondary: '#f0f0f0',
          },
        },
      }}
    />
    <RouterProvider router={router} />
  </React.StrictMode>,
)
