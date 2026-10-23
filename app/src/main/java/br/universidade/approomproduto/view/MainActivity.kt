package br.universidade.approomproduto.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import br.universidade.approomproduto.dao.ProductDao
import br.universidade.approomproduto.databinding.ActivityMainBinding
import br.universidade.approomproduto.db.AppDatabase
import br.universidade.approomproduto.model.Product
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration() // A8: recria o banco (apaga dados) ao mudar a versao
            .build()
        productDao = db.productDao()

        // ---------- Inserir (A1, A2, A8) ----------
        binding.btnInserirProduto.setOnClickListener {
            val name = binding.editProductName.text.toString().trim()
            val priceText = binding.editProductPrice.text.toString().trim()
            val description = binding.editProductDescription.text.toString()
            val quantity = binding.editProductQuantity.text.toString().toIntOrNull() ?: 0

            if (name.isEmpty()) {
                binding.editProductName.error = "Informe o nome"
                return@setOnClickListener
            }

            // A2: preco vazio, zero ou negativo nao e permitido
            val price = priceText.toDoubleOrNull()
            if (price == null || price <= 0.0) {
                binding.editProductPrice.error = "Informe um preço válido"
                return@setOnClickListener
            }

            val product = Product(
                name = name,
                price = price,
                description = description,
                quantity = quantity
            )

            lifecycleScope.launch {
                productDao.insertAll(product)
                Log.i("TAG", "Produto inserido: $name - R$$price")
                Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()

                // A1: limpar campos e devolver o foco ao campo Nome
                binding.editProductName.text.clear()
                binding.editProductPrice.text.clear()
                binding.editProductQuantity.text.clear()
                binding.editProductDescription.text.clear()
                binding.editProductName.requestFocus()
            }
        }

        // ---------- Listar (A3, A4) ----------
        binding.btnListarProdutos.setOnClickListener {
            lifecycleScope.launch {
                mostrarProdutos(productDao.getAll())
            }
        }

        // ---------- Buscar por nome (A5) ----------
        binding.btnBuscar.setOnClickListener {
            val texto = binding.editBusca.text.toString().trim()
            lifecycleScope.launch {
                mostrarProdutos(productDao.buscarPorNome("%$texto%"))
            }
        }

        // ---------- Atualizar preco (A7) ----------
        binding.btnAtualizarPreco.setOnClickListener {
            val id = binding.editIdAtualizar.text.toString().toIntOrNull()
            val novoPreco = binding.editNovoPreco.text.toString().toDoubleOrNull()

            if (id == null) {
                binding.editIdAtualizar.error = "Informe o ID"
                return@setOnClickListener
            }
            if (novoPreco == null || novoPreco <= 0.0) {
                binding.editNovoPreco.error = "Informe um preço válido"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val produto = productDao.getById(id)
                if (produto == null) {
                    Toast.makeText(this@MainActivity, "Produto não encontrado", Toast.LENGTH_SHORT).show()
                } else {
                    productDao.update(produto.copy(price = novoPreco))
                    Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
                    mostrarProdutos(productDao.getAll())
                }
            }
        }

        // ---------- Excluir pelo ID (A6 extra) ----------
        binding.btnExcluirPorId.setOnClickListener {
            val id = binding.editIdAtualizar.text.toString().toIntOrNull()
            if (id == null) {
                binding.editIdAtualizar.error = "Informe o ID"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val removidos = productDao.deleteById(id)
                val msg = if (removidos > 0) "Produto excluído!" else "Produto não encontrado"
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                mostrarProdutos(productDao.getAll())
            }
        }

        // ---------- Excluir todos (A6) ----------
        binding.btnExcluirTodos.setOnClickListener {
            lifecycleScope.launch {
                productDao.deleteAll()
                Toast.makeText(this@MainActivity, "Todos os produtos excluídos", Toast.LENGTH_SHORT).show()
                mostrarProdutos(productDao.getAll())
            }
        }
    }

    // A3 + A4: mostra a lista no TextView, com total e soma dos precos
    private fun mostrarProdutos(products: List<Product>) {
        if (products.isEmpty()) {
            binding.txtResultado.text = "Nenhum produto encontrado."
            Log.i("TAG", "Nenhum produto encontrado.")
            return
        }

        val sb = StringBuilder()
        products.forEach {
            val linha = "ID ${it.id} | ${it.name} | R$ ${String.format(Locale.US, "%.2f", it.price)} | " +
                    "Qtd: ${it.quantity} | ${it.description}"
            sb.appendLine(linha)
            Log.i("TAG", linha)
        }

        val total = products.sumOf { it.price }
        sb.appendLine()
        sb.appendLine("Produtos cadastrados: ${products.size}")
        sb.append("Soma dos preços: R$ ${String.format(Locale.US, "%.2f", total)}")
        binding.txtResultado.text = sb.toString()
    }
}
