```mermaid
graph TD
    subgraph Client_Layer ["Tầng Giao Diện (Client)"]
        Web[Trang Web Khách hàng]
        App[App Quét QR Nhân viên]
        Admin[Trang Quản trị Rạp]
    end

    subgraph Network_Layer ["Tầng Mạng"]
        Gateway[API Gateway / Load Balancer]
    end

    subgraph Backend_Layer ["Tầng Xử Lý (Backend)"]
        TicketSvc[Ticket Service - Đặt vé]
        UserSvc[User Service - Tài khoản]
        PaymentSvc[Payment Service - Thanh toán]
    end

    subgraph Data_Layer ["Tầng Dữ Liệu (Database)"]
        DB[(Cơ sở dữ liệu SQL)]
    end

    %% Các luồng kết nối
    Web -->|Gửi Request| Gateway
    App -->|Gửi Request| Gateway
    Admin -->|Gửi Request| Gateway

    Gateway -->|Điều hướng| TicketSvc
    Gateway -->|Điều hướng| UserSvc
    Gateway -->|Điều hướng| PaymentSvc

    TicketSvc -->|Đọc/Ghi dữ liệu| DB
    UserSvc -->|Đọc/Ghi dữ liệu| DB
    PaymentSvc -->|Đọc/Ghi dữ liệu| DB