package com.example.phuongstore.customer.service;

import com.example.phuongstore.common.dto.PageResponse;
import com.example.phuongstore.common.exception.BusinessException;
import com.example.phuongstore.common.exception.ResourceNotFoundException;
import com.example.phuongstore.customer.dto.CustomerRequest;
import com.example.phuongstore.customer.dto.CustomerResponse;
import com.example.phuongstore.customer.entity.Customer;
import com.example.phuongstore.customer.mapper.CustomerMapper;
import com.example.phuongstore.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            String phone = request.phoneNumber().trim();
            if (customerRepository.existsByPhoneNumber(phone)) {
                throw new BusinessException("Số điện thoại '" + phone + "' đã tồn tại trong hệ thống. Vui lòng kiểm tra lại.");
            }
        }
        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findOrThrow(id));
    }

    public PageResponse<CustomerResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        var resultPage = (keyword == null || keyword.isBlank())
            ? customerRepository.findAll(pageable)
            : customerRepository.searchByKeyword(keyword.trim(), pageable);

        return PageResponse.from(resultPage.map(customerMapper::toResponse));
    }

    public List<CustomerResponse> getAll() {
        return customerRepository.findAll(Sort.by("fullName").ascending())
            .stream()
            .map(customerMapper::toResponse)
            .toList();
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOrThrow(id);
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            String phone = request.phoneNumber().trim();
            if (customerRepository.existsByPhoneNumberAndIdNot(phone, id)) {
                throw new BusinessException("Số điện thoại '" + phone + "' đã được sử dụng bởi khách hàng khác.");
            }
        }
        customerMapper.updateEntity(customer, request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Khách hàng", id);
        }
        customerRepository.deleteById(id);
    }

    Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", id));
    }
}
