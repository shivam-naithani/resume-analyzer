import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Analyze from './pages/Analyze';
import AnalysisResult from './pages/AnalysisResult';
import History from './pages/History';
import HealthCheck from './pages/HealthCheck';
import NotFound from './pages/NotFound';

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/status" element={<HealthCheck />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/analyze" element={<Analyze />} />
          <Route path="/analyses/:id" element={<AnalysisResult />} />
          <Route path="/history" element={<History />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}

export default App;