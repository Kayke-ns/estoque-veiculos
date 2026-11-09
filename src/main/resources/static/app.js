const API = '/api';
const $ = (s) => document.querySelector(s);
const brl = (v) => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const km = (v) => Number(v).toLocaleString('pt-BR') + ' km';

let marcas = [];
let modelos = [];

async function api(caminho, opcoes = {}) {
  const resp = await fetch(API + caminho, {
    headers: { 'Content-Type': 'application/json' },
    ...opcoes,
  });
  if (resp.status === 204) return null;
  const corpo = await resp.json().catch(() => null);
  if (!resp.ok) throw new Error(corpo?.detalhes?.join(' | ') || 'Erro ' + resp.status);
  return corpo;
}

function toast(msg, falha = false) {
  const t = $('#toast');
  t.textContent = msg;
  t.className = 'on' + (falha ? ' falha' : '');
  clearTimeout(t._timer);
  t._timer = setTimeout(() => (t.className = ''), 2800);
}

document.querySelectorAll('.aba').forEach((b) =>
  b.addEventListener('click', () => {
    document.querySelectorAll('.aba').forEach((x) => x.classList.toggle('ativa', x === b));
    $('#aba-veiculos').hidden = b.dataset.aba !== 'veiculos';
    $('#aba-catalogo').hidden = b.dataset.aba !== 'catalogo';
  })
);

async function carregarCatalogo() {
  [marcas, modelos] = await Promise.all([api('/marcas'), api('/modelos')]);
  const optMarcas = marcas.map((m) => `<option value="${m.id}">${m.nome}</option>`).join('');
  $('#filtros [name=marcaId]').innerHTML = '<option value="">Todas</option>' + optMarcas;
  $('#form-modelo [name=marcaId]').innerHTML = optMarcas;
  atualizarModelosFiltro();
  $('#form-veiculo [name=modeloId]').innerHTML = modelos
    .map((m) => `<option value="${m.id}">${m.marcaNome} ${m.nome}</option>`).join('');

  $('#lista-marcas').innerHTML = marcas.map((m) => `
    <li><span>${m.nome} <small>${m.paisOrigem || ''}</small></span>
    <button class="mini perigo" onclick="removerMarca(${m.id})">Remover</button></li>`).join('');
  $('#lista-modelos').innerHTML = modelos.map((m) => `
    <li><span>${m.marcaNome} ${m.nome} <small>${m.tipo}</small></span>
    <button class="mini perigo" onclick="removerModelo(${m.id})">Remover</button></li>`).join('');
}

function atualizarModelosFiltro() {
  const marcaId = $('#filtros [name=marcaId]').value;
  const lista = modelos.filter((m) => !marcaId || String(m.marcaId) === marcaId);
  $('#filtros [name=modeloId]').innerHTML = '<option value="">Todos</option>' +
    lista.map((m) => `<option value="${m.id}">${m.nome}</option>`).join('');
}
$('#filtros [name=marcaId]').addEventListener('change', atualizarModelosFiltro);

$('#form-marca').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    await api('/marcas', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(e.target))) });
    e.target.reset();
    toast('Marca cadastrada');
    carregarCatalogo();
  } catch (err) { toast(err.message, true); }
});

$('#form-modelo').addEventListener('submit', async (e) => {
  e.preventDefault();
  const dados = Object.fromEntries(new FormData(e.target));
  dados.marcaId = Number(dados.marcaId);
  try {
    await api('/modelos', { method: 'POST', body: JSON.stringify(dados) });
    e.target.reset();
    toast('Modelo cadastrado');
    carregarCatalogo();
  } catch (err) { toast(err.message, true); }
});

async function removerMarca(id) {
  if (!confirm('Remover esta marca?')) return;
  try { await api('/marcas/' + id, { method: 'DELETE' }); toast('Marca removida'); carregarCatalogo(); }
  catch (err) { toast(err.message, true); }
}
async function removerModelo(id) {
  if (!confirm('Remover este modelo?')) return;
  try { await api('/modelos/' + id, { method: 'DELETE' }); toast('Modelo removido'); carregarCatalogo(); }
  catch (err) { toast(err.message, true); }
}

async function carregarResumo() {
  const r = await api('/veiculos/resumo');
  $('#resumo').innerHTML = `
    <div class="card"><span>Total</span><strong>${r.total}</strong></div>
    <div class="card"><span>Disponíveis</span><strong>${r.disponiveis}</strong></div>
    <div class="card"><span>Reservados</span><strong>${r.reservados}</strong></div>
    <div class="card"><span>Vendidos</span><strong>${r.vendidos}</strong></div>
    <div class="card"><span>Valor disponível</span><strong>${brl(r.valorEmEstoque)}</strong></div>`;
}

async function carregarVeiculos() {
  const params = new URLSearchParams();
  for (const [k, v] of new FormData($('#filtros'))) if (v) params.append(k, v);
  try {
    const lista = await api('/veiculos?' + params);
    $('#contador').textContent = `${lista.length} veículo(s) encontrado(s)`;
    $('#lista').innerHTML = lista.length ? lista.map((v) => `
      <tr>
        <td>${v.id}</td>
        <td><strong>${v.marca}</strong> ${v.modelo}</td>
        <td>${v.tipo}</td>
        <td>${v.anoFabricacao}</td>
        <td>${v.cor}</td>
        <td class="num">${brl(v.preco)}</td>
        <td class="num">${km(v.quilometragem)}</td>
        <td>${v.placa || '-'}</td>
        <td><span class="status s-${v.status}">${v.status}</span></td>
        <td><div class="acoes">
          <button class="mini" onclick='editar(${JSON.stringify(v)})'>Editar</button>
          ${v.status !== 'VENDIDO' ? `<button class="mini" onclick="vender(${v.id})">Vender</button>` : ''}
          <button class="mini perigo" onclick="removerVeiculo(${v.id})">Remover</button>
        </div></td>
      </tr>`).join('') : '<tr><td colspan="10" class="vazio">Nenhum veículo encontrado com esses filtros.</td></tr>';
  } catch (err) { toast(err.message, true); }
}

$('#filtros').addEventListener('submit', (e) => { e.preventDefault(); carregarVeiculos(); });
$('#filtros').addEventListener('reset', () => setTimeout(() => { atualizarModelosFiltro(); carregarVeiculos(); }));

function abrirModal(titulo, v = null) {
  const f = $('#form-veiculo');
  f.reset();
  $('#erro-form').textContent = '';
  $('#modal-titulo').textContent = titulo;
  f.elements['id'].value = v?.id || '';
  if (v) {
    f.modeloId.value = v.modeloId;
    f.anoFabricacao.value = v.anoFabricacao;
    f.cor.value = v.cor;
    f.preco.value = v.preco;
    f.quilometragem.value = v.quilometragem;
    f.placa.value = v.placa || '';
    f.status.value = v.status;
  }
  $('#modal').showModal();
}
$('#btn-novo').addEventListener('click', () => abrirModal('Novo veículo'));
$('#btn-cancelar').addEventListener('click', () => $('#modal').close());
function editar(v) { abrirModal('Editar veículo #' + v.id, v); }

$('#form-veiculo').addEventListener('submit', async (e) => {
  e.preventDefault();
  const d = Object.fromEntries(new FormData(e.target));
  const id = d.id;
  delete d.id;
  d.modeloId = Number(d.modeloId);
  d.anoFabricacao = Number(d.anoFabricacao);
  d.preco = Number(d.preco);
  d.quilometragem = Number(d.quilometragem);
  try {
    await api('/veiculos' + (id ? '/' + id : ''), { method: id ? 'PUT' : 'POST', body: JSON.stringify(d) });
    $('#modal').close();
    toast(id ? 'Veículo atualizado' : 'Veículo cadastrado');
    atualizarTudo();
  } catch (err) { $('#erro-form').textContent = err.message; }
});

async function vender(id) {
  try { await api(`/veiculos/${id}/vender`, { method: 'POST' }); toast('Veículo marcado como vendido'); atualizarTudo(); }
  catch (err) { toast(err.message, true); }
}
async function removerVeiculo(id) {
  if (!confirm('Remover este veículo do estoque?')) return;
  try { await api('/veiculos/' + id, { method: 'DELETE' }); toast('Veículo removido'); atualizarTudo(); }
  catch (err) { toast(err.message, true); }
}

function atualizarTudo() { carregarResumo(); carregarVeiculos(); }

carregarCatalogo().then(atualizarTudo);
