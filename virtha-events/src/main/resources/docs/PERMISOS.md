# Sistema de Permisos de VIRTHA-Events

## Permisos de Duración de Baneo

El plugin VIRTHA-Events incluye un sistema de permisos que permite controlar la duración del baneo cuando un jugador llega al mínimo de corazones permitidos en el modo HealthSteal.

### Permisos Disponibles

| Permiso | Descripción |
|---------|-------------|
| `virtha.ban.duration.exempt` | Exime al jugador de ser baneado al llegar al mínimo de corazones |
| `virtha.ban.duration.1` | Establece la duración del baneo a 1 hora |
| `virtha.ban.duration.2` | Establece la duración del baneo a 2 horas |
| `virtha.ban.duration.3` | Establece la duración del baneo a 3 horas |
| `virtha.ban.duration.4` | Establece la duración del baneo a 4 horas |
| `virtha.ban.duration.6` | Establece la duración del baneo a 6 horas |
| `virtha.ban.duration.12` | Establece la duración del baneo a 12 horas |
| `virtha.ban.duration.24` | Establece la duración del baneo a 24 horas |

### Funcionamiento

Cuando un jugador alcanza el mínimo de corazones permitidos (5 corazones), el sistema verifica si tiene alguno de estos permisos:

1. Si el jugador tiene el permiso `virtha.ban.duration.exempt`, no será baneado y solo recibirá un mensaje de advertencia.

2. Si el jugador tiene alguno de los permisos de duración específica (por ejemplo, `virtha.ban.duration.3`), será baneado por esa cantidad de horas, independientemente del número de veces que haya sido baneado anteriormente.

3. Si el jugador no tiene ningún permiso específico, se aplicará la duración por defecto: 6 horas multiplicadas por el número de veces que ha sido baneado.

### Ejemplos

- Un jugador con el permiso `virtha.ban.duration.exempt` nunca será baneado por llegar al mínimo de corazones.

- Un jugador con el permiso `virtha.ban.duration.2` será baneado por 2 horas cada vez que llegue al mínimo de corazones.

- Un jugador sin permisos específicos será baneado por 6 horas la primera vez, 12 horas la segunda vez, 18 horas la tercera vez, etc.

### Configuración en Plugins de Permisos

Puedes configurar estos permisos en cualquier plugin de gestión de permisos compatible con Bukkit/Spigot, como LuckPerms, PermissionsEx, etc.

Ejemplo con LuckPerms:

```
/lp user <jugador> permission set virtha.ban.duration.3 true
```

Esto establecerá la duración del baneo a 3 horas para el jugador especificado.