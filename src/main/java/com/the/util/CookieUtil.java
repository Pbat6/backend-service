package com.the.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CookieUtil {
    /**
     * Tạo một Cookie và thêm vào HttpServletResponse.
     * @param response      Đối tượng HttpServletResponse để thêm cookie vào.
     * @param name          Tên của cookie.
     * @param value         Giá trị của cookie.
     * @param maxAgeSeconds Thời gian sống của cookie tính bằng giây.
     */
    public void create(HttpServletResponse response, String name, String value, Integer maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // Cực kỳ quan trọng: Ngăn JavaScript phía client truy cập cookie
        cookie.setMaxAge(maxAgeSeconds); // Thời gian sống của cookie
        cookie.setPath("/"); // Áp dụng cho toàn bộ domain
        // cookie.setSecure(true); // Chỉ gửi cookie qua HTTPS (nên bật khi deploy production)
        response.addCookie(cookie);
    }

    /**
     * Lấy giá trị của một cookie từ HttpServletRequest.
     * @param request Đối tượng HttpServletRequest chứa cookie.
     * @param name    Tên của cookie cần lấy.
     * @return Optional chứa giá trị của cookie nếu tìm thấy, ngược lại trả về Optional rỗng.
     */
    public Optional<String> get(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Xóa một cookie bằng cách đặt thời gian sống của nó về 0.
     * @param response Đối tượng HttpServletResponse để cập nhật cookie.
     * @param name     Tên của cookie cần xóa.
     */
    public void clear(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null); // Giá trị có thể là null
        cookie.setMaxAge(0); // Đặt thời gian sống về 0 để trình duyệt xóa nó
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
