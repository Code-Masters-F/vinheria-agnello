namespace VinheriaApi.Models
{
    public class Produto
    {
        public long Id { get; set; }
        public long VinheriaId { get; set; } = 1; // Default for simplicity if needed
        public string Nome { get; set; } = string.Empty;
        public string Tipo { get; set; } = "Vinho"; // Default
        public string? Descricao { get; set; }
        public decimal Preco { get; set; }
        public int Estoque { get; set; }
    }
}
