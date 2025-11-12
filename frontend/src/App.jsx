import { Link, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Signup from "./pages/Signup.jsx";
import Dashboard from "./pages/Dashboard.jsx";

function Navbar() {
  const token = localStorage.getItem("token");
  const navigate = useNavigate();
  const logout = () => { localStorage.removeItem("token"); navigate("/login"); };
  return (
    <nav style={{display:"flex",gap:12,padding:12,borderBottom:"1px solid #eee"}}>
      <Link to="/">Home</Link>
      <Link to="/login">Login</Link>
      <Link to="/signup">Signup</Link>
      <Link to="/dashboard">Dashboard</Link>
      {token && <button onClick={logout}>Logout</button>}
    </nav>
  );
}

function Home() {
  return (
    <section style={{padding:24}}>
      <h1>React + Spring Boot</h1>
      <p>Welcome. Use Login/Signup to continue.</p>
    </section>
  );
}

// Guard for protected pages
function PrivateRoute({ children }) {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login/>} />
        <Route path="/signup" element={<Signup/>} />
        <Route path="/dashboard" element={<PrivateRoute><Dashboard/></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
