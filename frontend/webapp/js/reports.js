function exportar(nome, formato) {
    const campoCliente = document.getElementById('filtroClienteRelatorio');
    const cliente = campoCliente ? campoCliente.value.trim() : '';
    const parametros = new URLSearchParams({ nome, formato, cliente });
    window.open(`/api/relatorios/exportar?${parametros.toString()}`, '_blank');
}
