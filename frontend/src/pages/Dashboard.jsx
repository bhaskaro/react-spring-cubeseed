import { useEffect, useState } from "react";
import { http } from "../api/http";

export default function Dashboard() {
  const [data, setData] = useState("Loading…");
  const [err, setErr] = useState("");
  useEffect(() => {
	  (async () => {
		try {
		  const me = await http("/secure/me", { auth: true }); // <-- auth:true
		  setData(JSON.stringify(me, null, 2));
		} catch (e) {
		  setErr(e.message); // will show 401 if token missing/invalid
		}
	  })();
	}, []);

  return (
    <main style={{padding:24}}>
      <h2>Dashboard</h2>
      {err ? <div style={{color:"crimson"}}>{err}</div> : <pre>{data}</pre>}
    </main>
  );
}
