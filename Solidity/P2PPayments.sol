//Este es el contrato que contiene toda la lógica de negocio. Debe heredar de UUPSUpgradeable y Initializable de OpenZeppelin.

// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol";
import "@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol";
import "@openzeppelin/contracts-upgradeable/utils/ReentrancyGuardUpgradeable.sol";
import "@openzeppelin/contracts-upgradeable/utils/structs/EnumerableSetUpgradeable.sol";
import "./interfaces/IP2PPayments.sol"; // Importa la interfaz

contract P2PPayments is Initializable, UUPSUpgradeable, OwnableUpgradeable, ReentrancyGuardUpgradeable, IP2PPayments {
    using EnumerableSetUpgradeable for EnumerableSetUpgradeable.Bytes32Set;

    enum PaymentStatus { Pending, Confirmed, Cancelled, Disputed, Resolved }

    struct Payment {
        address sender;
        address receiver;
        uint256 amount;
        bytes32 messageHash; // Hash de un mensaje off-chain si es necesario
        uint256 deadline;    // Timestamp límite para confirmar/cancelar
        uint256 timestamp;   // Timestamp de creación del pago
        PaymentStatus status;
        bool exists;         // Para verificar si un paymentId existe
    }

    mapping(bytes32 => Payment) private _payments;
    mapping(address => EnumerableSetUpgradeable.Bytes32Set) private _userPayments;

    // Slot de almacenamiento para el UUPSUpgradeable (importante para evitar colisiones)
    // No es necesario definirlo explícitamente si se usan las librerías de OZ correctamente.

    /// @custom:oz-upgrades-unsafe-allow constructor
    constructor() {
        // El constructor del contrato de implementación NO debe inicializar variables de estado,
        // solo el `initializer` lo hace a través del proxy.
        _disableInitializers(); // Importante para UUPS
    }

    function initialize(address owner_) public initializer {
        __Ownable_init(owner_); // Inicializa el Ownable
        __UUPSUpgradeable_init(); // Inicializa el UUPSUpgradeable
        // Otros inicializadores si los hay
    }

    // Función para verificar si la nueva implementación es autorizada para la actualización
    function _authorizeUpgrade(address newImplementation) internal override onlyOwner {}

    // Implementación de las funciones de la interfaz IP2PPayments

    function initiatePayment(
        address _receiver,
        uint256 _amount,
        bytes32 _messageHash,
        uint256 _deadline
    ) external payable override nonReentrant returns (bytes32 paymentId) {
        require(msg.value == _amount, "P2PPayments: Amount sent does not match payment amount");
        require(_receiver != address(0), "P2PPayments: Invalid receiver address");
        require(_receiver != msg.sender, "P2PPayments: Cannot pay yourself");
        require(_deadline > block.timestamp, "P2PPayments: Deadline must be in the future");

        paymentId = keccak256(abi.encodePacked(msg.sender, _receiver, _amount, _messageHash, block.timestamp, _deadline));

        require(!_payments[paymentId].exists, "P2PPayments: Payment ID already exists");

        _payments[paymentId] = Payment({
            sender: msg.sender,
            receiver: _receiver,
            amount: _amount,
            messageHash: _messageHash,
            deadline: _deadline,
            timestamp: block.timestamp,
            status: PaymentStatus.Pending,
            exists: true
        });

        _userPayments[msg.sender].add(paymentId);
        _userPayments[_receiver].add(paymentId);

        emit PaymentInitiated(paymentId, msg.sender, _receiver, _amount, _messageHash, _deadline);
    }

    function confirmPayment(bytes32 _paymentId) external override nonReentrant {
        Payment storage payment = _payments[_paymentId];
        require(payment.exists, "P2PPayments: Payment does not exist");
        require(payment.receiver == msg.sender, "P2PPayments: Only receiver can confirm");
        require(payment.status == PaymentStatus.Pending, "P2PPayments: Payment is not pending");

        payment.status = PaymentStatus.Confirmed;
        payable(payment.receiver).transfer(payment.amount); // Envía los fondos al receptor

        emit PaymentConfirmed(_paymentId, msg.sender);
    }

    function cancelPayment(bytes32 _paymentId) external override nonReentrant {
        Payment storage payment = _payments[_paymentId];
        require(payment.exists, "P2PPayments: Payment does not exist");
        require(payment.sender == msg.sender, "P2PPayments: Only sender can cancel");
        require(payment.status == PaymentStatus.Pending, "P2PPayments: Payment is not pending");
        require(block.timestamp > payment.deadline, "P2PPayments: Cannot cancel before deadline"); // Solo se puede cancelar después del deadline

        payment.status = PaymentStatus.Cancelled;
        payable(payment.sender).transfer(payment.amount); // Devuelve los fondos al remitente

        emit PaymentCancelled(_paymentId, msg.sender);
    }

    function openDispute(bytes32 _paymentId) external override nonReentrant {
        Payment storage payment = _payments[_paymentId];
        require(payment.exists, "P2PPayments: Payment does not exist");
        require(payment.status == PaymentStatus.Pending, "P2PPayments: Payment is not pending");
        require(msg.sender == payment.sender || msg.sender == payment.receiver, "P2PPayments: Only sender or receiver can open dispute");
        require(block.timestamp > payment.deadline, "P2PPayments: Dispute can only be opened after deadline");

        payment.status = PaymentStatus.Disputed;
        emit DisputeOpened(_paymentId, msg.sender);
    }

    // Esta función para resolver disputas debería ser llamada por un árbitro o un sistema de gobernanza
    function releaseFundsFromDispute(
        bytes32 _paymentId,
        address _winner
    ) external override onlyOwner nonReentrant { // Solo el propietario (o árbitro) puede llamar
        Payment storage payment = _payments[_paymentId];
        require(payment.exists, "P2PPayments: Payment does not exist");
        require(payment.status == PaymentStatus.Disputed, "P2PPayments: Payment is not under dispute");
        require(_winner == payment.sender || _winner == payment.receiver, "P2PPayments: Winner must be sender or receiver");

        payment.status = PaymentStatus.Resolved;
        payable(_winner).transfer(payment.amount);

        emit FundsReleasedFromDispute(_paymentId, _winner, payment.amount);
    }

    function getPaymentDetails(
        bytes32 _paymentId
    )
        external
        view
        override
        returns (
            address sender,
            address receiver,
            uint256 amount,
            bytes32 messageHash,
            uint256 deadline,
            uint256 timestamp,
            string memory status
        )
    {
        Payment storage payment = _payments[_paymentId];
        require(payment.exists, "P2PPayments: Payment does not exist");

        return (
            payment.sender,
            payment.receiver,
            payment.amount,
            payment.messageHash,
            payment.deadline,
            payment.timestamp,
            _paymentStatusToString(payment.status)
        );
    }

    function getUserPayments(
        address _user
    ) external view override returns (bytes32[] memory) {
        return _userPayments[_user].values();
    }

    function _paymentStatusToString(PaymentStatus _status) internal pure returns (string memory) {
        if (_status == PaymentStatus.Pending) return "Pending";
        if (_status == PaymentStatus.Confirmed) return "Confirmed";
        if (_status == PaymentStatus.Cancelled) return "Cancelled";
        if (_status == PaymentStatus.Disputed) return "Disputed";
        if (_status == PaymentStatus.Resolved) return "Resolved";
        return "Unknown";
    }
}
