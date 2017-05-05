# FEUP_COMP

## Alterações
- Novo tipo de nó : ARRAY (tive que mudar porque o array poderia ser do tipo a[a][b][c] e da maneira que estava não dava para distinguir os indexes do valor do assignement
- Pela razão acima, o identifier dos assignements/variabledeclar vão ser o 1º nó desse nó,
## Parser
- Tratar de declaração de variáveis na mesma linha;
Exemplo:
```javascript
"type": "VariableDeclaration",
    "declarations": [
        {
            "type": "VariableDeclarator",
            "id": {
                "type": "Identifier",
                "name": "a"
            },
            "init": {
                "type": "Literal",
                "value": 5,
                "raw": "5"
            }
        },
        {
            "type": "VariableDeclarator",
            "id": {
                "type": "Identifier",
                "name": "c"
            },
            "init": {
                "type": "Literal",
                "value": 7,
                "raw": "7"
            }
        }
    ],
    "kind": "var"
}
```
