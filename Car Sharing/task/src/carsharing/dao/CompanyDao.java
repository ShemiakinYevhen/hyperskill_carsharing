package carsharing.dao;

import carsharing.models.Company;

import java.util.List;

public interface CompanyDao {

    List<Company> getAllCompanies();

    boolean addCompany(Company newCompany);

    boolean updateCompany();

    boolean deleteCompany();

    int getIndexOfCompanyByName(String companyName);
}
