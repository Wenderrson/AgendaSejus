package agenda;

import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JFrame {
    private JComboBox<String> usuarioComboBox;
    private JPasswordField senhaField;
    private JLabel senhaLabel;

    public LoginGUI() {
        setTitle("Login");
        setSize(300, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 1));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));


        usuarioComboBox = new JComboBox<>(new String[]{"Sejus", "Administrador"});
        senhaField = new JPasswordField();
        senhaLabel = new JLabel("Senha:");
        JButton loginButton = new JButton("Entrar");

        add(new JLabel("Usuário:"));
        add(usuarioComboBox);
        add(senhaLabel);
        add(senhaField);
        add(loginButton);

        usuarioComboBox.addActionListener(e -> alternarSenha());
        loginButton.addActionListener(e -> autenticar());

        // 🔽 Quando pressionar Enter no campo de senha, faz login
        senhaField.addActionListener(e -> autenticar());

        alternarSenha();
        setVisible(true);
    }

    private void alternarSenha() {
        boolean admin = usuarioComboBox.getSelectedItem().equals("Administrador");
        senhaLabel.setVisible(admin);
        senhaField.setVisible(admin);
    }

    private void autenticar() {
        String usuario = (String) usuarioComboBox.getSelectedItem();
        String senha = new String(senhaField.getPassword()).trim();

        if (usuario.equals("Sejus")) {
            new AgendaGUI(false);
            dispose();
        } else if (usuario.equals("Administrador") && senha.equals("admin")) {
            new AgendaGUI(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Senha inválida para administrador!");
        }
    }
}
