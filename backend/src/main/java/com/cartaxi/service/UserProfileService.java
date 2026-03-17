package com.cartaxi.service;

import com.cartaxi.dto.user.profile.UserProfileUpdateDTO;
import com.cartaxi.vo.user.profile.UserProfileVO;

public interface UserProfileService {

    UserProfileVO getCurrentProfile();

    UserProfileVO updateCurrentProfile(UserProfileUpdateDTO updateDTO);
}
