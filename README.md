# FEUP_COMP

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
