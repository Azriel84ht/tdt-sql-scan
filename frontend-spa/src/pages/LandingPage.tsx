import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';

const LandingPage: React.FC = () => {
  return (
    <div className="landing-page">
      <Navbar />
      <main className="hero-section">
        <h1 className="hero-title">Visualiza y Audita tus Procesos ETL</h1>
        <p className="hero-description">
          Una herramienta para analizar, desglosar y entender tus flujos de datos SQL de una manera más clara y eficiente.
        </p>
        <Link to="/login" className="hero-button">
          Empezar Ahora
        </Link>
      </main>
      <Footer />
    </div>
  );
};

export default LandingPage;