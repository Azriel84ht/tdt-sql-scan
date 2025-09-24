import { Route, Routes } from 'react-router-dom';
import LoginPage from 'pages/login';
import HomePage from 'pages/home';
import { ProtectedRoute } from './ProtectedRoute';

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}></Route>
    </Routes>
  );
};