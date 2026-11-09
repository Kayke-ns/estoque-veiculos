// Abre o sistema no Chrome (modo headless) e salva as telas usadas no relatorio em docs/prints.
// Uso: node scripts/capturar-telas.mjs  (com a aplicacao rodando em http://localhost:8080)
// Requer Node 22+ (WebSocket nativo) e Google Chrome instalado.
import { spawn } from 'node:child_process';
import { mkdirSync, writeFileSync, readFileSync, existsSync, mkdtempSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';

const BASE = 'http://localhost:8080';
const OUT = resolve('docs/prints');
mkdirSync(OUT, { recursive: true });

const CHROME = [
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/Applications/Chromium.app/Contents/MacOS/Chromium',
  '/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge',
  '/Applications/Brave Browser.app/Contents/MacOS/Brave Browser',
].find(existsSync);
if (!CHROME) throw new Error('Chrome nao encontrado em /Applications');

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const PORT = 9333;
const chrome = spawn(CHROME, [
  '--headless=new', `--remote-debugging-port=${PORT}`, '--no-first-run', '--no-default-browser-check',
  '--hide-scrollbars', `--user-data-dir=${mkdtempSync(join(tmpdir(), 'cap-'))}`, 'about:blank',
], { stdio: 'ignore' });

let alvo;
for (let i = 0; i < 50 && !alvo; i++) {
  await sleep(300);
  try {
    const lista = await (await fetch(`http://127.0.0.1:${PORT}/json/list`)).json();
    alvo = lista.find((t) => t.type === 'page');
  } catch { /* chrome ainda subindo */ }
}
if (!alvo) throw new Error('Nao foi possivel conectar ao Chrome');

const ws = new WebSocket(alvo.webSocketDebuggerUrl);
await new Promise((r) => (ws.onopen = r));
let seq = 0;
const pend = new Map();
ws.onmessage = (ev) => {
  const m = JSON.parse(ev.data);
  if (m.id && pend.has(m.id)) {
    pend.get(m.id)(m);
    pend.delete(m.id);
  }
};
const cdp = (method, params = {}) => new Promise((ok, falha) => {
  const id = ++seq;
  pend.set(id, (m) => (m.error ? falha(new Error(method + ': ' + m.error.message)) : ok(m.result)));
  ws.send(JSON.stringify({ id, method, params }));
});
const js = async (expr) => {
  const r = await cdp('Runtime.evaluate', { expression: expr, awaitPromise: true, returnByValue: true });
  if (r.exceptionDetails) throw new Error('JS: ' + JSON.stringify(r.exceptionDetails.exception?.description));
  return r.result.value;
};

const LARG = 1440, ALT = 900;
await cdp('Emulation.setDeviceMetricsOverride', { width: LARG, height: ALT, deviceScaleFactor: 2, mobile: false });

async function abrir(url, espera = 1800) {
  await cdp('Page.navigate', { url });
  await sleep(espera);
}
async function foto(nome, paginaInteira = false) {
  let clip;
  if (paginaInteira) {
    const altura = await js('Math.max(document.documentElement.scrollHeight, document.body.scrollHeight)');
    clip = { x: 0, y: 0, width: LARG, height: Math.min(altura, 2200), scale: 1 };
  }
  const r = await cdp('Page.captureScreenshot', { format: 'png', captureBeyondViewport: !!paginaInteira, ...(clip ? { clip } : {}) });
  writeFileSync(join(OUT, nome), Buffer.from(r.data, 'base64'));
  console.log('  salvo', nome);
}
const preencher = (sel, campos) => js(`(() => { const f = document.querySelector(${JSON.stringify(sel)});
  for (const [k, v] of Object.entries(${JSON.stringify(campos)})) f.elements[k].value = v; return true; })()`);

console.log('Capturando telas...');

// 1. Tela inicial
await abrir(BASE + '/');
await js('window.confirm = () => true; true');
await foto('figura-1.png', true);

// 7. Cadastro de marca e modelo (aba Marcas e Modelos)
await js(`document.querySelector('[data-aba=catalogo]').click(); true`);
await preencher('#form-marca', { nome: 'Hyundai', paisOrigem: 'Coreia do Sul' });
await js(`document.querySelector('#form-marca').requestSubmit(); true`);
await sleep(1200);
const idHyundai = await js(`fetch('/api/marcas').then(r => r.json()).then(l => l.find(m => m.nome === 'Hyundai').id)`);
await preencher('#form-modelo', { nome: 'HB20', tipo: 'HATCH', marcaId: String(idHyundai) });
await js(`document.querySelector('#form-modelo').requestSubmit(); true`);
await sleep(1500);
await foto('figura-7.png', true);

// 2. Cadastro de veiculo (modal preenchido)
await js(`document.querySelector('[data-aba=veiculos]').click(); true`);
const idHB20 = await js(`fetch('/api/modelos').then(r => r.json()).then(l => l.find(m => m.nome === 'HB20').id)`);
await js(`document.querySelector('#btn-novo').click(); true`);
await sleep(400);
await preencher('#form-veiculo', { modeloId: String(idHB20), anoFabricacao: '2024', cor: 'Azul', preco: '84900', quilometragem: '0', placa: 'HBV2A24', status: 'DISPONIVEL' });
await sleep(300);
await foto('figura-2.png');
await js(`document.querySelector('#form-veiculo').requestSubmit(); true`);
await sleep(1500);
await foto('figura-2b.png', true);

// 3. Filtro combinado
await preencher('#filtros', { status: 'DISPONIVEL', precoMax: '120000', anoMin: '2020', ordenarPor: 'preco', direcao: 'asc' });
await js(`document.querySelector('#filtros').requestSubmit(); true`);
await sleep(1200);
await foto('figura-3.png', true);

// 4. Filtro por tipo
await js(`document.querySelector('#filtros').reset(); true`);
await sleep(800);
await preencher('#filtros', { tipo: 'SUV' });
await js(`document.querySelector('#filtros').requestSubmit(); true`);
await sleep(1200);
await foto('figura-4.png', true);
await js(`document.querySelector('#filtros').reset(); true`);
await sleep(1200);

// 5. Edicao (UPDATE) do HB20
const hb20 = await js(`fetch('/api/veiculos?busca=HB20').then(r => r.json()).then(l => l[0])`);
await js(`editar(${JSON.stringify(hb20)}); true`);
await sleep(400);
await preencher('#form-veiculo', { preco: '82900', quilometragem: '50' });
await foto('figura-5.png');
await js(`document.querySelector('#form-veiculo').requestSubmit(); true`);
await sleep(1500);

// 9. Validacao: tentar diminuir a quilometragem de outro veiculo
const corolla = await js(`fetch('/api/veiculos?busca=corolla&ordenarPor=preco&direcao=desc').then(r => r.json()).then(l => l[0])`);
await js(`editar(${JSON.stringify(corolla)}); true`);
await sleep(400);
await preencher('#form-veiculo', { quilometragem: '10' });
await js(`document.querySelector('#form-veiculo').requestSubmit(); true`);
await sleep(1200);
await foto('figura-9.png');
await js(`document.querySelector('#modal').close(); true`);

// 6. Venda e remocao
const polo = await js(`fetch('/api/veiculos?busca=polo').then(r => r.json()).then(l => l[0].id)`);
await js(`vender(${polo}); true`);
await sleep(1200);
await js(`removerVeiculo(${hb20.id}); true`);
await sleep(700);
await foto('figura-6.png', true);

// 8. API respondendo JSON
await abrir(BASE + '/api/veiculos?status=DISPONIVEL&precoMax=120000&ordenarPor=preco&direcao=asc', 1500);
await foto('figura-8.png');

// 10. Resultado dos testes automatizados (saida real do "mvn test")
if (existsSync('docs/testes.log')) {
  const log = readFileSync('docs/testes.log', 'utf8')
    .split('\n')
    .filter((l) => /Tests run|BUILD|Running br|T E S T S|-{10,}|Results|ERROR|FAIL/.test(l))
    .join('\n')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;');
  const html = `<!doctype html><meta charset=utf-8><body style="margin:0;background:#1e1e1e">
    <div style="font:13px Menlo,monospace;color:#d4d4d4;padding:16px 20px">
    <div style="color:#8a8a8a;margin-bottom:10px">$ mvn test</div><pre style="margin:0;white-space:pre-wrap">${log
      .replace(/BUILD SUCCESS/g, '<span style="color:#4ec97b;font-weight:bold">BUILD SUCCESS</span>')
      .replace(/Failures: 0, Errors: 0/g, '<span style="color:#4ec97b">Failures: 0, Errors: 0</span>')}</pre></div>`;
  const arq = resolve('docs/prints/testes.html');
  writeFileSync(arq, html);
  await cdp('Emulation.setDeviceMetricsOverride', { width: 1100, height: 420, deviceScaleFactor: 2, mobile: false });
  await abrir('file://' + arq, 800);
  await foto('figura-10.png');
}

console.log('Telas salvas em', OUT);
ws.close();
chrome.kill();
process.exit(0);
