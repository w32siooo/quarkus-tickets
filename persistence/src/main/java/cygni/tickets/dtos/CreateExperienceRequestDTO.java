package cygni.tickets.dtos;


public record CreateExperienceRequestDTO(String artist, String venue, String date, int price, int seats) {
}

