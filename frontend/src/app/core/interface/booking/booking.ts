export interface IBooking {
  id: string;
  transactionCode: string | null;
  arrivalDate: string;
  nights: number;
  departureDate: string;
  clientId: string;
  apartmentId: string;
  amountPeople: number;
  totalPrice: number;
  version: number;
}

export interface BookingUpdateRequest {
  arrivalDate: string;
  departureDate: string;
  nights: number;
  clientId: string;
  apartmentId: string;
  amountPeople: number;
  version: number;
}

export interface BookingCreateRequest {
  arrivalDate: string;
  departureDate: string;
  nights: number;
  amountPeople: number;
  apartmentId: string;
  client: {
    clientName: string;
    clientLastName: string;
    clientDni: string;
    clientPhone: string;
  };
}
