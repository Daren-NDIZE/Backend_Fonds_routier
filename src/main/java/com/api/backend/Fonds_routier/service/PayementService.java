package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.MessageDTO;
import com.api.backend.Fonds_routier.enums.Region;
import com.api.backend.Fonds_routier.model.Payement;
import com.api.backend.Fonds_routier.model.Programme;
import com.api.backend.Fonds_routier.model.Projet;
import com.api.backend.Fonds_routier.model.ProjetMINHDU;
import com.api.backend.Fonds_routier.repository.PayementRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PayementService {

    @Autowired
    PayementRepository payementRepository;

    public void savePayement(Projet projet, Payement payement){

        payement.setProjet(projet);
        payement.setDate(new Date());

        payement.setM_TVA(Math.round(19.25*payement.getM_HTVA())/100.0);
        payement.setM_TTC(Math.round(payement.getM_HTVA()*119.25)/100.0 );
        payement.setNap( Math.round( payement.getM_HTVA()*(100- payement.getAir()) )/100.0 );
        payement.setM_AIR(Math.round(payement.getAir()*payement.getM_HTVA())/100.0);

        payementRepository.save(payement);
    }

    public Payement findPayement(long id){

        return payementRepository.findById(id).orElse(null);
    }

    public void updatePayement(Payement payement, Payement update){

        payement.setDecompte(update.getDecompte());
        payement.setN_marche(update.getN_marche());
        payement.setObservation(update.getObservation());
        payement.setM_HTVA(update.getM_HTVA());
        payement.setAir(update.getAir());

        payement.setM_TVA(Math.round(19.25*update.getM_HTVA())/100.0);
        payement.setM_TTC(Math.round(update.getM_HTVA()*119.25)/100.0 );
        payement.setNap( Math.round( update.getM_HTVA()*(100- update.getAir()) )/100.0 );
        payement.setM_AIR(Math.round(update.getAir()*update.getM_HTVA())/100.0);

        payementRepository.save(payement);
    }

    public void deletePayement(long id){

        payementRepository.deleteById(id);
    }

    public MessageDTO importpayement(InputStream inputStream , Projet projet) throws IOException {
        List<Payement> list = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue; // Skip the header row
            }

            Payement payement=new Payement();
            payement.setProjet(projet);

            for (int i = 1; i <= 4; i++) {
                Cell cell = CellUtil.getCell(row, i);
                if (cell != null) {
                    switch (i) {
                        case 1:
                            if (cell.getCellType() == CellType.STRING) {
                                    payement.setDecompte(cell.getStringCellValue());
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.STRING ) {
                                payement.setN_marche(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                payement.setM_HTVA(cell.getNumericCellValue());

                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                payement.setM_HTVA((long) cellValue.getNumberValue());
                            }
                            else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                payement.setAir(cell.getNumericCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));

                            }
                            break;

                        default:
                            // Handle unknown columns or ignore them
                            break;
                    }
                }
            }

            payement.setDate(new Date());

            payement.setM_TVA(Math.round(19.25*payement.getM_HTVA())/100.0);
            payement.setM_TTC(Math.round(payement.getM_HTVA()*119.25)/100.0 );
            payement.setNap( Math.round( payement.getM_HTVA()*(100- payement.getAir()) )/100.0 );
            payement.setM_AIR(Math.round(payement.getAir()*payement.getM_HTVA())/100.0);

            list.add(payement);
        }

        payementRepository.saveAll(list);


        if (workbook != null) {
            workbook.close();
        }

        return new MessageDTO("succes","Importation réussi");

    }

    private String getCellLetter(Cell cell) {
        // Convertir l'index de colonne en lettre (A, B, C, ...)
        StringBuilder columnLetter = new StringBuilder();
        int dividend = cell.getColumnIndex() + 1;
        int modulo;
        int indexRow=cell.getRowIndex()+1;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            columnLetter.insert(0, (char) (65 + modulo));
            dividend = (dividend - modulo) / 26;
        }

        return columnLetter.toString()+String.valueOf(indexRow);
    }
}
