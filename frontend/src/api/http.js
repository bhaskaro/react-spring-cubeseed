// src/api/http.js
export async function http(path, { method="GET", body, auth=false } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (auth) {
    const t = localStorage.getItem("token");
    if (t) headers.Authorization = `Bearer ${t}`;
  }
  const res = await fetch(`/api${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });
  if (!res.ok) throw new Error(String(res.status));
  return res.headers.get("content-type")?.includes("json") ? res.json() : res.text();
}
