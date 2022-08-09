package carsharing.impl;

import carsharing.dao.CompanyDao;
import carsharing.models.Company;

import java.util.ArrayList;
import java.util.List;

public class CompanyDaoImpl implements CompanyDao {

    private List<Company> companies;

    public CompanyDaoImpl() {
        companies = new ArrayList<>();
    }

    public CompanyDaoImpl(List<Company> companies) {
        this.companies = companies;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    @Override
    public List<Company> getAllCompanies() {
        return companies;
    }

    @Override
    public boolean addCompany(Company newCompany) {
        if (companies.contains(newCompany)) return false;
        else return companies.add(newCompany);
    }

    @Override
    public boolean updateCompany() {
        return false;
    }

    @Override
    public boolean deleteCompany() {
        return false;
    }

    @Override
    public int getIndexOfCompanyByName(String companyName) {
        for (int i = 0; i < companies.size(); i++) {
            if (companies.get(i).getName().equals(companyName)) return i;
        }
        return -1;
    }
}
