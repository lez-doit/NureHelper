package com.example.nurehelper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimetableUnit {
    String name;
    String link;
    LocalTime timeStart;
    LocalTime timeEnd;
}
