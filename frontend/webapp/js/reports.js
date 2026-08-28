function exportar(nome, formato) {
    window.open(`/api/relatorios/exportar?nome=${nome}&formato=${formato}`, '_blank');
}
