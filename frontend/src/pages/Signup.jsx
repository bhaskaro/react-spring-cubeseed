import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { http } from "../api/http";

export default function Signup() {
  const [form, setForm] = useState({
    username: "", email: "", password: "", fullName: "", userType: "BUSINESS"
  });
  const [err, setErr] = useState("");
  const navigate = useNavigate();

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setErr("");
    try {
      // backend endpoint: POST /api/auth/signup -> { id, ... } or { token }
      await http("/auth/signup", { method:"POST", body: form });
      // optional: auto-login after signup by calling /auth/login
      navigate("/login");
    } catch (e) { setErr(e.message); }
  };

  return (
    <main style={{padding:24, maxWidth:560}}>
      <h2>Signup</h2>
      <form onSubmit={submit} style={{display:"grid", gap:10}}>
        <input name="fullName" placeholder="Full name" value={form.fullName} onChange={onChange} required/>
        <input name="username" placeholder="Username" value={form.username} onChange={onChange} required/>
        <input name="email" type="email" placeholder="Email" value={form.email} onChange={onChange} required/>
        <input name="password" type="password" placeholder="Password" value={form.password} onChange={onChange} required/>
        <select name="userType" value={form.userType} onChange={onChange}>
          <option value="BUSINESS">Business</option>
          <option value="RETAILER">Retailer</option>
        </select>
        <button type="submit">Create account</button>
        {err && <div style={{color:"crimson"}}>{err}</div>}
      </form>
    </main>
  );
}
