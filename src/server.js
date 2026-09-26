import http from "node:http";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const publicDir = path.join(__dirname, "..", "public");
const port = Number(process.env.PORT ?? 3456);
const host = process.env.HOST ?? "0.0.0.0";

/** @type {Array<{ id: string; destination: string; startDate: string; travelers: number }>} */
const trips = [];

function sendJson(res, status, body) {
  res.writeHead(status, { "Content-Type": "application/json" });
  res.end(JSON.stringify(body));
}

async function serveStatic(res, filePath, contentType) {
  try {
    const data = await readFile(filePath);
    res.writeHead(200, { "Content-Type": contentType });
    res.end(data);
  } catch {
    res.writeHead(404);
    res.end("Not found");
  }
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url ?? "/", `http://${req.headers.host}`);

  if (url.pathname === "/api/health") {
    return sendJson(res, 200, { status: "ok", service: "navora-api", trips: trips.length });
  }

  if (url.pathname === "/api/trips" && req.method === "GET") {
    return sendJson(res, 200, { trips });
  }

  if (url.pathname === "/api/trips" && req.method === "POST") {
    const chunks = [];
    for await (const chunk of req) {
      chunks.push(chunk);
    }
    let payload;
    try {
      payload = JSON.parse(Buffer.concat(chunks).toString("utf8"));
    } catch {
      return sendJson(res, 400, { error: "Invalid JSON body" });
    }

    const destination = String(payload.destination ?? "").trim();
    const startDate = String(payload.startDate ?? "").trim();
    const travelers = Number(payload.travelers ?? 1);

    if (!destination || !startDate || !Number.isFinite(travelers) || travelers < 1) {
      return sendJson(res, 400, { error: "destination, startDate, and travelers are required" });
    }

    const trip = {
      id: `trip-${trips.length + 1}`,
      destination,
      startDate,
      travelers,
    };
    trips.push(trip);
    return sendJson(res, 201, trip);
  }

  if (url.pathname === "/" || url.pathname === "/index.html") {
    return serveStatic(res, path.join(publicDir, "index.html"), "text/html; charset=utf-8");
  }

  res.writeHead(404);
  res.end("Not found");
});

server.listen(port, host, () => {
  console.log(`Navora dev server listening on http://${host}:${port}`);
});
