package com.sleekydz86.kqueryds.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import lombok.Getter
import lombok.Setter

@Entity
@Getter @Setter
class Hello {

    @Id @GeneratedValue
    var id: Long? =null
}