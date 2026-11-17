export interface IDashboard {
  id: string;
  transactionCode: string | null;
  clientId: string;
  clientName: string;
  clientLastName: string;
  apartmentId: string;
  apartmentName: string;
  arrivalDate: string; // formato yyyy-MM-dd
  departureDate: string; // formato yyyy-MM-dd
  nights: number; // cantidad de noches
  amountPeople: number; // cantidad de personas
  totalPrice: number; // precio total de la reserva
  status: 'ACTIVE' | 'CANCELLED' | 'ARCHIVED'; // estado de la reserva
  version: number; // versión para control de concurrencia optimista
}
