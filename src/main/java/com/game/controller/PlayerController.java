package com.game.controller;

import com.game.entity.Player;
import com.game.model.PlayerInfo;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(@Autowired PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping()
    public List<PlayerInfo> getAll(@RequestParam(required = false) Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize) {
        pageNumber = isNull(pageNumber) ? 0 : pageNumber;
        pageSize = isNull(pageSize) ? 3 : pageSize;

        List<Player> players = playerService.getAll(pageNumber, pageSize);
        return players.stream().map(PlayerController::toPlayerInfo).collect(Collectors.toList());
    }

    @GetMapping("/count")
    public Integer getAllCount() {
        return playerService.getAllCount();
    }

    @PostMapping
    public ResponseEntity<PlayerInfo> createPlayer(@RequestBody PlayerInfo info) {
        if (StringUtils.isEmpty(info.name) || info.name.length() > 12) return ResponseEntity.badRequest().build();
        if (info.title.length() > 30) return ResponseEntity.badRequest().build();
        if (isNull(info.race)) return ResponseEntity.badRequest().build();
        if (isNull(info.profession)) return ResponseEntity.badRequest().build();
        if (isNull(info.birthday) || info.birthday < 0) return ResponseEntity.badRequest().build();

        LocalDate localDate = new Date(info.birthday).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        if (year < 2000 || year > 3000) return ResponseEntity.badRequest().build();

        boolean banned = !isNull(info.banned) && info.banned;

        Player player = playerService.createPlayer(info.name, info.title, info.race, info.profession, info.birthday, banned, info.level);
        return ResponseEntity.status(HttpStatus.OK).body(toPlayerInfo(player));
    }

    @PostMapping("/{ID}")
    public ResponseEntity<PlayerInfo> updatePlayer(@PathVariable("ID") long id,
                                                   @RequestBody PlayerInfo info) {
        if (id <= 0) return ResponseEntity.badRequest().build();
        if (nonNull(info.name) && (info.name.length() > 12 || info.name.isEmpty())) return ResponseEntity.badRequest().build();
        if (nonNull(info.title) && info.title.length() > 30) return ResponseEntity.badRequest().build();

        Player player = playerService.updatePlayer(id, info.name, info.title, info.race, info.profession, info.banned);
        if (isNull(player)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(toPlayerInfo(player));
        }
    }

    @DeleteMapping("/{ID}")
    public ResponseEntity<Void> delete(@PathVariable("ID") long id) {
        if (id <= 0) return ResponseEntity.badRequest().build();

        Player player = playerService.delete(id);
        if (isNull(player)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    private static PlayerInfo toPlayerInfo(Player player) {
        if (isNull(player)) return null;

        PlayerInfo result = new PlayerInfo();
        result.id = player.getId();
        result.name = player.getName();
        result.title = player.getTitle();
        result.race = player.getRace();
        result.profession = player.getProfession();
        result.birthday = player.getBirthday().getTime();
        result.banned = player.getBanned();
        result.level = player.getLevel();
        return result;
    }
}