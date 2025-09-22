import React from 'react';
import { useAuthStore } from '../store/authStore';

const HomePage: React.FC = () => {
  const { logout } = useAuthStore();

  return (
    <div>
      <h1>Home Page</h1>
      <p>Welcome! You are logged in.</p>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

export default HomePage;
