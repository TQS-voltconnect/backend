package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.dtos.BrandDTO;

import java.util.List;

public interface BrandService {
    List<BrandDTO> getAllBrands();
    BrandDTO getBrandById(String id);
    List<BrandDTO> getBrandByName(String name);
    void importAllBrands(boolean force);
}
