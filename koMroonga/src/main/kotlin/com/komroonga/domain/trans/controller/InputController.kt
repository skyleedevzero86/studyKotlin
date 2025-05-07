package com.komroonga.domain.trans.controller

import com.komroonga.domain.trans.entity.InputEntity
import com.komroonga.domain.trans.service.InputService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/input")
class InputController(private val service: InputService) { //채팅페이지

    //채팅으로 보여줌
    @GetMapping
    fun showInputPage(model: Model): String {
        model.addAttribute("inputs", service.getAllInputs())
        return "embed/EmbeddingView"
    }

    @PostMapping
    @ResponseBody
    fun saveInput(@RequestParam text: String): InputEntity {
        return service.saveInput(text)
    }

    @GetMapping("/data/{id}")
    @ResponseBody
    fun getData(@PathVariable id: Long): InputEntity? = service.getInputById(id)

}