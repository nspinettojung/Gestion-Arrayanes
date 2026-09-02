export interface IDashboard {
  id: string;
  clientId: string;
  clientName: string;
  clientLastName: string;
  apartmentName: string;
  arrivalDate: string; // formato yyyy-MM-dd
  departureDate: string; // formato yyyy-MM-dd
  amountPeople: number; // cantidad de personas
  totalPrice: number; // precio total de la reserva
}
