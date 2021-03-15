package com.github.hteph.repository.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
public abstract class StellarObject implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private String archiveID;
	private OrbitalFacts orbitalFacts;
}
