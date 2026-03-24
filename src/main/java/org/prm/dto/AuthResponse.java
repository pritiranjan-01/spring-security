package org.prm.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
   private String access_token;
   private String refresh_token;
}
