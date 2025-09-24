import React from 'react';
import { Link } from 'react-router-dom';

const Navbar: React.FC = () => {
  return (
    <nav>
      <div>
        <span>TDT SQL Scan</span>
      </div>
      <div>
        <Link to="/login">
          <button>Iniciar Sesión</button>
        </Link>
      </div>
    </nav>
  );
};

export default Navbar;