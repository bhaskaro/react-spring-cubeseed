import React from "react";
import ReactDOM from "react-dom/client";
import { HashRouter } from "react-router-dom"; // HashRouter = zero server config
import App from "./App.jsx";

ReactDOM.createRoot(document.getElementById("root")).render(
  <HashRouter>
    <App />
  </HashRouter>
);
