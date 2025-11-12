import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { http } from "../api/http";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setErr("");
    try {
      // backend endpoint: POST /api/auth/login  -> { token }
      const data = await http("/auth/login", {
        method: "POST",
        body: { username, password }
      });
      localStorage.setItem("token", data.token);
      navigate("/dashboard");
    } catch (e) {
      setErr(e.message);
    }
  };

  return (
    <main style={{padding:24, maxWidth:420}}>
      <h2>Login</h2>
      <form onSubmit={submit} style={{display:"grid", gap:10}}>
        <input placeholder="Username or Email" value={username} onChange={e=>setUsername(e.target.value)} required/>
        <input type="password" placeholder="Password" value={password} onChange={e=>setPassword(e.target.value)} required/>
        <button type="submit">Sign in</button>
        {err && <div style={{color:"crimson"}}>{err}</div>}
      </form>
    </main>
  );
}
