using System.Collections.Generic;
using System.Threading.Tasks;
using VinheriaApi.Models;

namespace VinheriaApi.Repositories
{
    public interface IProdutoRepository
    {
        Task<IEnumerable<Produto>> GetAllAsync();
        Task<Produto?> GetByIdAsync(long id);
        Task<long> CreateAsync(Produto produto);
        Task<bool> UpdateAsync(Produto produto);
        Task<bool> DeleteAsync(long id);
    }
}
