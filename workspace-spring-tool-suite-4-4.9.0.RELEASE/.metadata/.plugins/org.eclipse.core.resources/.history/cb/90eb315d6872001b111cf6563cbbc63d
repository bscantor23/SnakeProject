package com.app.items.clientes;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.app.items.models.Producto;

@FeignClient(name = "servicio-productos")
public interface ProductoClienteRest {

	@GetMapping("")
	public List<Producto> listar();
	
	@GetMapping("/{id}")
	public Optional<Producto> ver(@PathVariable Long id);
}
