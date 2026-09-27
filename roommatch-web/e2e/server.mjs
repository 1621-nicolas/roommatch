// Serves the production build with the proposed deployment CSP. No live API.
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
const root = path.resolve('dist/roommatch-web/browser');
const nginx = fs.readFileSync('../deployment/nginx.conf', 'utf8');
const csp = nginx.match(/add_header Content-Security-Policy "([^"]+)"/)[1];
if (!fs.existsSync(path.join(root, 'index.html'))) throw new Error('Run npm run build before browser tests.');
http.createServer((req, res) => {
  res.setHeader('Content-Security-Policy', csp);
  res.setHeader('Referrer-Policy', 'no-referrer');
  res.setHeader('X-Content-Type-Options', 'nosniff');
  let pathname;
  try { pathname = decodeURIComponent(new URL(req.url, 'http://127.0.0.1').pathname); }
  catch { res.writeHead(400); res.end(); return; }
  if (pathname.startsWith('/api/')) {
    res.writeHead(503, {'Content-Type': 'application/json'});
    res.end(JSON.stringify({status: 'error', message: 'API not configured in UI test server', data: null})); return;
  }
  const requested = path.join(root, pathname);
  if (!requested.startsWith(root + path.sep) && requested !== root) {res.writeHead(403); res.end(); return;}
  const file = fs.existsSync(requested) && fs.statSync(requested).isFile() ? requested : path.join(root, 'index.html');
  res.setHeader('Content-Type', ({'.html': 'text/html', '.js': 'application/javascript', '.css': 'text/css', '.ico': 'image/x-icon', '.woff2': 'font/woff2'})[path.extname(file)] || 'application/octet-stream');
  fs.createReadStream(file).pipe(res);
}).listen(4173, '127.0.0.1');
