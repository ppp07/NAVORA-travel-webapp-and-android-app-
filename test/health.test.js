import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import { once } from "node:events";
import test from "node:test";

test("GET /api/health returns ok", async () => {
  const port = 34567;
  const child = spawn(process.execPath, ["src/server.js"], {
    env: { ...process.env, PORT: String(port), HOST: "127.0.0.1" },
    cwd: new URL("..", import.meta.url).pathname,
  });

  await new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error("server did not start")), 5000);
    child.stdout.on("data", (chunk) => {
      if (chunk.toString().includes("listening")) {
        clearTimeout(timeout);
        resolve();
      }
    });
    child.on("error", reject);
  }).then(async () => {
    const res = await fetch(`http://127.0.0.1:${port}/api/health`);
    assert.equal(res.status, 200);
    const body = await res.json();
    assert.equal(body.status, "ok");
    assert.equal(body.service, "navora-api");
  });

  child.kill();
  await once(child, "exit");
});
