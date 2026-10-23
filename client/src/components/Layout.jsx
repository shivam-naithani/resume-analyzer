import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';

const currentYear = new Date().getFullYear();

export default function Layout() {
  return (
    <div className="app">
      <Navbar />
      <main className="container page-content">
        <Outlet />
      </main>
      <footer className="footer">
        <div className="container">&copy; {currentYear} Resume Analyzer</div>
      </footer>
    </div>
  );
}