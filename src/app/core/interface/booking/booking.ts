export interface IBooking {
  id: string;
  arrivalDate: string;
  nights: number;
  departureDate: string;
  clientId: string;
  apartmentId: string;
  bookingPrice: number;
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
