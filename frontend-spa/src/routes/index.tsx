import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import HomePage from '../pages/HomePage';
import ProtectedRoute from './ProtectedRoute';

export const routes: RouteObject[] = [
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <HomePage />,
  },
  {
    element: <ProtectedRoute />,
    children: [],
  },
];

export const router = createBrowserRouter(routes);
