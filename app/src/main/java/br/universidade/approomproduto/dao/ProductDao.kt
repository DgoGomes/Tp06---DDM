package br.universidade.approomproduto.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.universidade.approomproduto.model.Product

@Dao
interface ProductDao {

    @Query("SELECT * FROM product")
    suspend fun getAll(): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    // A5: buscar por nome
    @Query("SELECT * FROM product WHERE name LIKE :nome")
    suspend fun buscarPorNome(nome: String): List<Product>

    // A6: excluir todos
    @Query("DELETE FROM product")
    suspend fun deleteAll()

    // A6 (extra): excluir um produto especifico
    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    // A7: atualizar produto
    @Update
    suspend fun update(product: Product)

    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getById(id: Int): Product?
}
