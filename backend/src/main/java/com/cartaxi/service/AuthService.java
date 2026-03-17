package com.cartaxi.service;

import com.cartaxi.dto.LoginRequest;
import com.cartaxi.vo.LoginVO;
import com.cartaxi.vo.ProfileVO;

public interface AuthService {

    LoginVO userLogin(LoginRequest request);

    LoginVO adminLogin(LoginRequest request);

    ProfileVO getProfile();
}
